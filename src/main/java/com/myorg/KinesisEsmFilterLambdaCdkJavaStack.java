package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.eventsources.KinesisEventSource;
import software.amazon.awscdk.services.kinesis.Stream;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static software.amazon.awscdk.BundlingOutput.ARCHIVED;

public class KinesisEsmFilterLambdaCdkJavaStack extends Stack {
    public KinesisEsmFilterLambdaCdkJavaStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public KinesisEsmFilterLambdaCdkJavaStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Function lambdaFn = Function.Builder.create(this, "MyLambda")
                .code(Code.fromInline("def lambda_handler(event,context): print(\"Hello\")"))
                .handler("index.lambda_handler")
                .timeout(Duration.minutes(0.5))
                .runtime(Runtime.PYTHON_3_9)
                .build();

        
        Stream stream = new Stream(this, "EsmFilterStream");

        lambdaFn.addEventSource(KinesisEventSource.Builder.create(stream)
               .batchSize(100) // default
               .startingPosition(StartingPosition.TRIM_HORIZON)
                .filters(List.of(FilterCriteria.filter(Map.of("stringEquals",FilterRule.isEqual("{ \"data\" : { \"order\" : { \"type\" : [ \"buy\" ] } } }")))))
               .build());

        new CfnOutput(this,"LambdaFunction", CfnOutputProps.builder().exportName("MyLambdaFunction").value(lambdaFn.getFunctionArn()).build());
        new CfnOutput(this,"KinesisLambda-KinesisStream", CfnOutputProps.builder().exportName("MyKinesisStream").value(lambdaFn.getFunctionArn()).build());
        
    }
}

