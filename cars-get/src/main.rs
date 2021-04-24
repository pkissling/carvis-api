use rusoto_dynamodb::{DynamoDb, DynamoDbClient, ScanInput};
use rusoto_core::Region;

use lambda_http::{
    handler,
    lambda_runtime::{self, Context},
    IntoResponse, Request, Response,
};

type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(func)).await?;
    Ok(())
}

async fn func(_: Request, _: Context) -> Result<impl IntoResponse, Error> {
    let client = DynamoDbClient::new(Region::EuWest1);

    let scan_input: ScanInput = ScanInput {
        attributes_to_get: Some(vec!("id".to_string(), "name".to_string())),
        conditional_operator: None,
        consistent_read: None,
        exclusive_start_key: None,
        expression_attribute_names: None,
        expression_attribute_values: None,
        filter_expression: None,
        index_name: None,
        limit: None,
        projection_expression: None,
        return_consumed_capacity: None,
        scan_filter: None,
        segment: None,
        select: None,
        table_name: "carvis-dev-cars".to_string(),
        total_segments: None,
    };


    return match client.scan(scan_input).await {
        Ok(output) => {
            let items = output.items.expect("items empty");
            let mut items_string = Vec::<String>::new();
            for item in items {
                let id = item.get("id").unwrap().s.as_ref().unwrap();
                let name = item.get("name").unwrap().s.as_ref().unwrap();
                items_string.push(format!("id = {}, name = {}", id, name))
            }

            Ok(format!("items = {}", items_string.join(",")).into_response())
        }
        Err(err) => Ok(Response::builder()
            .status(400)
            .body(err.to_string().into())
            .expect("failed to render response"))
    };
}