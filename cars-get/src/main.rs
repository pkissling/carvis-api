use rusoto_dynamodb::{DynamoDb, DynamoDbClient, ListTablesInput};
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
    let list_tables_input: ListTablesInput = Default::default();

    return match client.list_tables(list_tables_input).await {
        Ok(output) => {
            let tables = output.table_names.expect("no tables");
            Ok(format!("tables, {}!", tables.join(",")).into_response())
        },
        Err(err) => Ok(Response::builder()
            .status(400)
            .body(err.to_string().into())
            .expect("failed to render response"))
    }
}