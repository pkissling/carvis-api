use lambda_http::{handler, lambda_runtime::{self, Context}, IntoResponse, Request, Response, RequestExt};
use rusoto_dynamodb::{DynamoDb, DynamoDbClient, GetItemInput, AttributeValue};
use rusoto_core::Region;
use std::env;
use std::collections::HashMap;
use lambda_http::http::StatusCode;
use model::{FooError};
use auth::User;
use lazy_static::lazy_static;

extern crate auth;

type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(func)).await?;
    Ok(())
}

lazy_static! {
    static ref TABLE_NAME: String = env::var("DYNAMODB_REQUESTS_TABLE_NAME").expect("env var 'DYNAMODB_REQUESTS_TABLE_NAME' not set");
    static ref AUTHORITY: String = env::var("AUTHORITY").expect("env var AUTHORITY not set");
    static ref DB_CLIENT: DynamoDbClient = DynamoDbClient::new(Region::EuWest1);
}

async fn func(req: Request, _: Context) -> Result<impl IntoResponse, Error> {
    let request_id = req.path_parameters().get("requestId").expect("requestId not present").to_string();

    let auth_result = auth::authenticate(&req, &AUTHORITY);

    if auth_result.is_err() {
        let err = auth_result.err().unwrap();
        return response(err.status_code, serde_json::to_string(&err).unwrap());
    }

    let user: User = auth_result.ok().unwrap();

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

    match DB_CLIENT.get_item(input).await {
        Ok(_) => response(200, serde_json::to_string(&user).unwrap()),
        Err(err) => response(500, serde_json::to_string(&FooError { message: err.to_string() }).unwrap())
    }
}

fn response(status_code: u16, payload: String) -> Result<impl IntoResponse, Error> {
    Ok(Response::builder()
        .status(StatusCode::from_u16(status_code).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR))
        .body(payload)
        .expect("failed to render response"))
}

