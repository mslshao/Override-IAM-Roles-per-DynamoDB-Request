package com.aws.codestar.projecttemplates.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.aws.codestar.projecttemplates.GatewayResponse;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class HelloWorldHandler implements RequestHandler<Object, Object> {

    public Object handleRequest(final Object input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        // Default (development) credential provider which will be used if not overridden.
        DynamoDbClient client = DynamoDbClient.builder().region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create()).build();

        // Example using AWS Session Credentials instead of Basic credentials
        StaticCredentialsProvider account1creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create("account1-key", "account1-secret"));
        StaticCredentialsProvider account2creds = StaticCredentialsProvider.create(
                AwsSessionCredentials.create("prod-key-id", "prod-secret-key", "session-token"));

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("BookId", AttributeValue.fromS("abcdef-341691"));
        item.put("Timestamp", AttributeValue.fromS("2024-01-24:18:25:00Z"));

        // Override request configuration with account-level AwsCredentialsProvider (BasicCredentials).
        final PutItemRequest account1_req = createPutItemRequest(item, account1creds);
        client.putItem(account1_req);

        // Override with account-level session credentials.
        final PutItemRequest account2_req = createPutItemRequest(item, account2creds);
        client.putItem(account2_req);
        
        return new GatewayResponse(new JSONObject().put("ItemWritten", item).toString(), headers, 200);
    }

    private AwsRequestOverrideConfiguration createReqOverrideConfig(final AwsCredentialsProvider awsCredentialsProvider) {
        return AwsRequestOverrideConfiguration.builder()
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }

    private PutItemRequest createPutItemRequest(final Map<String, AttributeValue> itemMap,
                                                final AwsCredentialsProvider credsProvider) {
        return PutItemRequest.builder()
                .item(itemMap)
                .overrideConfiguration(createReqOverrideConfig(credsProvider))
                .build();
    }
}