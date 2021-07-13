use rusoto_dynamodb::{PutItemInput};
use rusoto_core::Region;
use rusoto_dynamodb::{DynamoDb, DynamoDbClient};
use lambda_http::{handler, lambda_runtime::{self, Context}, Request, Response, Body};
use std::env;
use model::request::RequestEntity;
use std::convert::TryInto;
use model::common::{LambdaError, LambdaResult};
use lazy_static::lazy_static;
use lambda_http::IntoResponse;

type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

lazy_static! {
    static ref TABLE_NAME: String = env::var("DYNAMODB_REQUESTS_TABLE_NAME").expect("env var 'DYNAMODB_REQUESTS_TABLE_NAME' not set");
    static ref AUTHORITY: String = env::var("AUTHORITY").expect("env var AUTHORITY not set");
    static ref DB_CLIENT: DynamoDbClient = DynamoDbClient::new(Region::EuWest1);
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(wrapper)).await?;
    Ok(())
}

async fn wrapper(req: Request, ctx: Context) -> Result<Response<Body>, Error> {
    let response = func(req, ctx).await
        .map(|r| r.into_response())
        .unwrap_or_else(|r| r.into_response());
    Ok(response)
}

async fn func(req: Request, _: Context) -> Result<LambdaResult, LambdaError> {
    let user = auth::authenticate(&req, &AUTHORITY)
        .map_err(|err| LambdaError::new(err.status_code, err.message))?;

    let dto = req.try_into()?;
    let entity = RequestEntity::create(dto, user.username);
    let id = entity.id.to_string();

    let input = PutItemInput {
        item: entity.into(),
        table_name: env::var("DYNAMODB_REQUESTS_TABLE_NAME").expect("env var 'DYNAMODB_REQUESTS_TABLE_NAME' not set"),
        ..Default::default()
    };

    Ok(match DB_CLIENT.put_item(input).await {
        Ok(_) => LambdaResult::new(201, id),
        Err(err) => LambdaResult::new(500, err.to_string())
    })
}

