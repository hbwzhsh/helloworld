/*
 * Copyright (c) 2007-2012 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zqh.cascading.impatient;

import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.FlowProcess;
import cascading.flow.hadoop2.Hadoop2MR1FlowConnector;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.operation.OperationCall;
import cascading.operation.aggregator.Count;
import cascading.operation.regex.RegexSplitGenerator;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.Retain;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

/**
 * http://docs.cascading.org/impatient/impatient3.html
 */
public class Scrub {

    public static void main(String[] args) {
        String docPath = args[0];
        String wcPath = args[1];

        Properties properties = new Properties();
        AppProps.setApplicationJarClass(properties, Scrub.class);
        FlowConnector flowConnector = new Hadoop2MR1FlowConnector(properties);

        // create source and sink taps
        // Hfs['TextDelimited[['doc-id','text']->[ALL]]'] ['data/rain.txt']']   ==> [{2}: 'doc_id', 'text']
        Tap docTap = new Hfs(new TextDelimited(true, "\t"), docPath);
        Tap wcTap = new Hfs(new TextDelimited(true, "\t"), wcPath);

        // specify a regex operation to split the "document" text lines into a token stream
        Fields token = new Fields("token");
        Fields text = new Fields("text");
        RegexSplitGenerator splitter = new RegexSplitGenerator(token, "[ \\[\\]\\(\\),.]");
        Fields fieldSelector = new Fields("doc_id", "token");
        // Each('token')[Regex[decl:'token'][args:1]]
        Pipe docPipe = new Each("token", text, splitter, fieldSelector);

        // define "ScrubFunction" to clean up the token stream
        Fields scrubArguments = new Fields("doc_id", "token");
        docPipe = new Each(docPipe, scrubArguments, new ScrubFunction(scrubArguments), Fields.RESULTS);

        // determine the word counts
        Pipe wcPipe = new Pipe("wc", docPipe);
        wcPipe = new Retain(wcPipe, token);
        wcPipe = new GroupBy(wcPipe, token);
        wcPipe = new Every(wcPipe, Fields.ALL, new Count(), Fields.ALL);

        // connect the taps, pipes, etc., into a flow
        FlowDef flowDef = FlowDef.flowDef()
                .setName("wc")
                .addSource(docPipe, docTap)
                .addTailSink(wcPipe, wcTap);

        // write a DOT file and run the flow
        Flow wcFlow = flowConnector.connect(flowDef);
        wcFlow.writeDOT("dot/wc.dot");
        wcFlow.complete();
    }
}

class ScrubFunction extends BaseOperation implements Function {
    public ScrubFunction(Fields fieldDeclaration) {
        super(2, fieldDeclaration);
    }

    public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
        TupleEntry argument = functionCall.getArguments();
        String doc_id = argument.getString(0);
        String token = scrubText(argument.getString(1));

        if (token.length() > 0) {
            Tuple result = new Tuple();
            result.add(doc_id);
            result.add(token);
            functionCall.getOutputCollector().add(result);
        }
    }

    public String scrubText(String text) {
        return text.trim().toLowerCase();
    }
}


