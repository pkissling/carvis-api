use rusoto_dynamodb::{PutItemInput, AttributeValue};
use rusoto_core::Region;
use rusoto_dynamodb::{DynamoDb, DynamoDbClient};
use lambda_http::{handler, lambda_runtime::{self, Context}, IntoResponse, Request, Response, Body};
use std::collections::HashMap;
use uuid::Uuid;
use std::env;
use std::time::SystemTime;
use serde::Deserialize;
use chrono::offset::Utc;
use chrono::DateTime;


type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(func)).await?;
    Ok(())
}

#[derive(Deserialize)]
struct RequestDto {
    name: String
}

struct RequestEntity {
    id: String,
    name: String,
    created_at: DateTime<Utc>,
    updated_at: Option<DateTime<Utc>>,
    created_by: String
}

trait Add {
    fn add(&mut self, key: &str, value: String);
}

impl Add for HashMap<String,AttributeValue> {
    fn add(&mut self, key: &str, value: String) {
        self.insert(key.to_string(), AttributeValue {
            s: Some(value),
            ..Default::default()
        });
    }
}

impl Into<HashMap<String,AttributeValue>> for RequestEntity {
    fn into(self) -> HashMap<String,AttributeValue> {
        let mut map: HashMap<String, AttributeValue> = HashMap::new();
        map.add("id", self.id);
        map.add("name", self.name);
        map.add("created_at", self.created_at.to_string());
        map
    }
}

impl RequestEntity {
    pub fn new(name: String, created_by: String) -> RequestEntity {
        RequestEntity {
            id: Uuid::new_v4().to_string(),
            created_at: SystemTime::now().into(),
            updated_at: None,
            name,
            created_by
        }
    }
}

pub(crate) async fn func(request: Request, _: Context) -> Result<impl IntoResponse, Error> {
    let client = DynamoDbClient::new(Region::EuWest1);
    let dto = to_dto(request.into_body());
    let entity = create_entity(dto);

    let input = PutItemInput {
        item: entity.into(),
        table_name: env::var("DYNAMODB_REQUESTS_TABLE_NAME").expect("env var 'DYNAMODB_REQUESTS_TABLE_NAME' not set"),
        ..Default::default()
    };

    Ok(match client.put_item(input).await {
        Ok(_) => Response::builder()
            .status(200)
            .body("ok".to_string())
            .expect("failed to render response"),
        Err(err) => Response::builder()
            .status(500)
            .body(err.to_string())
            .expect("failed to render response")
    })
}

fn to_dto(body: Body) -> RequestDto {
    let text = match body {
        Body::Text(t) => Some(t),
        _ => panic!("no payload")
    };

    let value = text.expect("no payload");
    serde_json::from_str(value.as_str()).expect("unable to parse JSON")
}

fn create_entity(r: RequestDto) -> RequestEntity {
    RequestEntity::new(r.name, "username".to_string())
}
