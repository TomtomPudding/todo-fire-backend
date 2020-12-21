package com.todo.admin.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverterFactory
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@EnableDynamoDBRepositories(
    basePackages = ["com.todo.admin.app.repository"],
    dynamoDBMapperConfigRef = "dynamoDBMapperConfig"
)
class DynamoDBConfig(
    private val environments: Environments
) {

    private val localEndpoint = "http://localhost:8000"
    private val localRegion = "ap-northeast-1"
    private val localDummyKey = "dummyKey"

    @Bean
    @Primary
    fun amazonDynamoDB() =
        AmazonDynamoDBClientBuilder
            .standard()
            .apply {
                if (!environments.isLocal) return@apply

                withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(localEndpoint, localRegion)
                )

                withCredentials(
                    AWSStaticCredentialsProvider(BasicAWSCredentials(localDummyKey, localDummyKey))
                )
            }.build()

    @Bean
    @Primary
    fun dynamoDBMapperConfig(): DynamoDBMapperConfig =
        DynamoDBMapperConfig.Builder()
            .withTypeConverterFactory(DynamoDBTypeConverterFactory.standard())
            .withTableNameResolver(DynamoDBMapperConfig.DefaultTableNameResolver.INSTANCE)
            .withTableNameOverride(
                DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix(environments.dynamoDBPrefix)
            )
            .build()

    @Bean
    @Primary
    fun dynamoDBMapper(amazonDynamoDB: AmazonDynamoDB, config: DynamoDBMapperConfig): DynamoDBMapper =
        DynamoDBMapper(amazonDynamoDB, config)
}
