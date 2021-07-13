use lambda_http::{handler, lambda_runtime::{self, Context}, Request, Response, RequestExt, Body, IntoResponse};
use rusoto_dynamodb::{DynamoDb, DynamoDbClient, GetItemInput, AttributeValue};
use rusoto_core::Region;
use std::env;
use std::collections::HashMap;
use model::common::{LambdaError, LambdaResult};
use model::request::RequestEntity;
use lazy_static::lazy_static;

extern crate auth;

type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(wrapper)).await?;
    Ok(())
}

lazy_static! {
    static ref TABLE_NAME: String = env::var("DYNAMODB_REQUESTS_TABLE_NAME").expect("env var 'DYNAMODB_REQUESTS_TABLE_NAME' not set");
    static ref AUTHORITY: String = env::var("AUTHORITY").expect("env var AUTHORITY not set");
    static ref DB_CLIENT: DynamoDbClient = DynamoDbClient::new(Region::EuWest1);
}

async fn wrapper(req: Request, ctx: Context) -> Result<Response<Body>, Error> {
    let response = func(req, ctx).await
        .map(|r|r.into_response())
        .unwrap_or_else(|r|r.into_response());
    Ok(response)
}
async fn func(req: Request, _: Context) -> Result<LambdaResult, LambdaError> {
    let request_id = req.path_parameters().get("requestId")
        .map(|request_id|request_id.to_string())
        .ok_or(LambdaError::new(400, "path parameter 'requestId' is not present".to_string()))?;

    let _user = auth::authenticate(&req, &AUTHORITY)
        .map_err(|err| LambdaError::new(err.status_code, err.message))?;
    let mut key_map = HashMap::new();
    key_map.insert("id".to_string(), AttributeValue {
        s: Some(request_id.to_string()),
        ..Default::default()
    });
    let input = GetItemInput {
        key: key_map,
        table_name: TABLE_NAME.to_string(),
        ..Default::default()
    };

    DB_CLIENT.get_item(input).await
        .map(|output|LambdaResult::new(200, serde_json::to_string(&RequestEntity::from(output.item.unwrap())).unwrap()))
        .map_err(|err| LambdaError::new(500, format!("{:?}", err)).into())
}

