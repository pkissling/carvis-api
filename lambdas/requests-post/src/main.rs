use rusoto_dynamodb::{PutItemInput};
use rusoto_core::Region;
use rusoto_dynamodb::{DynamoDb, DynamoDbClient};
use lambda_http::{handler, lambda_runtime::{self, Context}, Request, Response};
use std::env;
use model::request::RequestEntity;
use std::convert::TryInto;
use model::error::CarvisError;
use lazy_static::lazy_static;

type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

lazy_static! {
    static ref TABLE_NAME: String = env::var("DYNAMODB_REQUESTS_TABLE_NAME").expect("env var 'DYNAMODB_REQUESTS_TABLE_NAME' not set");
    static ref AUTHORITY: String = env::var("AUTHORITY").expect("env var AUTHORITY not set");
    static ref DB_CLIENT: DynamoDbClient = DynamoDbClient::new(Region::EuWest1);
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(func)).await?;
    Ok(())
}

async fn func(req: Request, _: Context) -> Result<Response<String>, Error> {
    let user = auth::authenticate(&req, &AUTHORITY)
        .map_err(|err|CarvisError::new(err.status_code, err.message))?;

    let dto = req.try_into()?;
    let entity = RequestEntity::create(dto, user.username);
    let id = entity.id.to_string();

    let input = PutItemInput {
        item: entity.into(),
        table_name: env::var("DYNAMODB_REQUESTS_TABLE_NAME").expect("env var 'DYNAMODB_REQUESTS_TABLE_NAME' not set"),
        ..Default::default()
    };

    Ok(match DB_CLIENT.put_item(input).await {
        Ok(_) => Response::builder()
            .status(200)
            .body(id)
            .expect("failed to render response"),
        Err(err) => Response::builder()
            .status(500)
            .body(err.to_string())
            .expect("failed to render response")
    })
}

