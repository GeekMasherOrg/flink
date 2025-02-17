/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.planner.plan.nodes.exec.batch;

import org.apache.flink.api.dag.Transformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.operators.OneInputStreamOperator;
import org.apache.flink.table.api.TableConfig;
import org.apache.flink.table.api.config.ExecutionConfigOptions;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.planner.codegen.CodeGeneratorContext;
import org.apache.flink.table.planner.codegen.agg.batch.HashWindowCodeGenerator;
import org.apache.flink.table.planner.codegen.agg.batch.WindowCodeGenerator;
import org.apache.flink.table.planner.delegation.PlannerBase;
import org.apache.flink.table.planner.plan.logical.LogicalWindow;
import org.apache.flink.table.planner.plan.nodes.exec.ExecEdge;
import org.apache.flink.table.planner.plan.nodes.exec.ExecNode;
import org.apache.flink.table.planner.plan.nodes.exec.ExecNodeBase;
import org.apache.flink.table.planner.plan.nodes.exec.ExecNodeContext;
import org.apache.flink.table.planner.plan.nodes.exec.InputProperty;
import org.apache.flink.table.planner.plan.nodes.exec.SingleTransformationTranslator;
import org.apache.flink.table.planner.plan.nodes.exec.utils.ExecNodeUtil;
import org.apache.flink.table.planner.plan.utils.AggregateInfoList;
import org.apache.flink.table.planner.plan.utils.AggregateUtil;
import org.apache.flink.table.planner.utils.JavaScalaConversionUtil;
import org.apache.flink.table.runtime.generated.GeneratedOperator;
import org.apache.flink.table.runtime.groupwindow.NamedWindowProperty;
import org.apache.flink.table.runtime.operators.CodeGenOperatorFactory;
import org.apache.flink.table.runtime.typeutils.InternalTypeInfo;
import org.apache.flink.table.types.logical.RowType;

import org.apache.calcite.rel.core.AggregateCall;

import java.util.Arrays;
import java.util.Collections;

/** Batch {@link ExecNode} for hash-based window aggregate operator. */
public class BatchExecHashWindowAggregate extends ExecNodeBase<RowData>
        implements BatchExecNode<RowData>, SingleTransformationTranslator<RowData> {

    private final int[] grouping;
    private final int[] auxGrouping;
    private final AggregateCall[] aggCalls;
    private final LogicalWindow window;
    private final int inputTimeFieldIndex;
    private final boolean inputTimeIsDate;
    private final NamedWindowProperty[] namedWindowProperties;
    private final RowType aggInputRowType;
    private final boolean enableAssignPane;
    private final boolean isMerge;
    private final boolean isFinal;

    public BatchExecHashWindowAggregate(
            int[] grouping,
            int[] auxGrouping,
            AggregateCall[] aggCalls,
            LogicalWindow window,
            int inputTimeFieldIndex,
            boolean inputTimeIsDate,
            NamedWindowProperty[] namedWindowProperties,
            RowType aggInputRowType,
            boolean enableAssignPane,
            boolean isMerge,
            boolean isFinal,
            InputProperty inputProperty,
            RowType outputType,
            String description) {
        super(
                ExecNodeContext.newNodeId(),
                ExecNodeContext.newContext(BatchExecHashWindowAggregate.class),
                Collections.singletonList(inputProperty),
                outputType,
                description);
        this.grouping = grouping;
        this.auxGrouping = auxGrouping;
        this.aggCalls = aggCalls;
        this.window = window;
        this.inputTimeFieldIndex = inputTimeFieldIndex;
        this.inputTimeIsDate = inputTimeIsDate;
        this.namedWindowProperties = namedWindowProperties;
        this.aggInputRowType = aggInputRowType;
        this.enableAssignPane = enableAssignPane;
        this.isMerge = isMerge;
        this.isFinal = isFinal;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Transformation<RowData> translateToPlanInternal(PlannerBase planner) {
        final ExecEdge inputEdge = getInputEdges().get(0);
        final Transformation<RowData> inputTransform =
                (Transformation<RowData>) inputEdge.translateToPlan(planner);

        final AggregateInfoList aggInfos =
                AggregateUtil.transformToBatchAggregateInfoList(
                        aggInputRowType,
                        JavaScalaConversionUtil.toScala(Arrays.asList(aggCalls)),
                        null, // aggCallNeedRetractions
                        null); // orderKeyIndexes
        final TableConfig tableConfig = planner.getTableConfig();
        final RowType inputRowType = (RowType) inputEdge.getOutputType();
        final HashWindowCodeGenerator hashWindowCodeGenerator =
                new HashWindowCodeGenerator(
                        new CodeGeneratorContext(tableConfig),
                        planner.getRelBuilder(),
                        window,
                        inputTimeFieldIndex,
                        inputTimeIsDate,
                        JavaScalaConversionUtil.toScala(Arrays.asList(namedWindowProperties)),
                        aggInfos,
                        inputRowType,
                        grouping,
                        auxGrouping,
                        enableAssignPane,
                        isMerge,
                        isFinal);
        final int groupBufferLimitSize =
                tableConfig
                        .getConfiguration()
                        .getInteger(ExecutionConfigOptions.TABLE_EXEC_WINDOW_AGG_BUFFER_SIZE_LIMIT);
        final Tuple2<Long, Long> windowSizeAndSlideSize = WindowCodeGenerator.getWindowDef(window);
        final GeneratedOperator<OneInputStreamOperator<RowData, RowData>> generatedOperator =
                hashWindowCodeGenerator.gen(
                        inputRowType,
                        (RowType) getOutputType(),
                        groupBufferLimitSize,
                        0, // windowStart
                        windowSizeAndSlideSize.f0,
                        windowSizeAndSlideSize.f1);

        final long managedMemory =
                tableConfig
                        .getConfiguration()
                        .get(ExecutionConfigOptions.TABLE_EXEC_RESOURCE_HASH_AGG_MEMORY)
                        .getBytes();
        return ExecNodeUtil.createOneInputTransformation(
                inputTransform,
                getOperatorName(tableConfig),
                getOperatorDescription(tableConfig),
                new CodeGenOperatorFactory<>(generatedOperator),
                InternalTypeInfo.of(getOutputType()),
                inputTransform.getParallelism(),
                managedMemory);
    }
}
