use serde::{Serialize, Deserialize};
use chrono::offset::Utc;
use chrono::DateTime;
use std::collections::HashMap;
use rusoto_dynamodb::AttributeValue;
use crate::common::LambdaError;
use std::convert::TryInto;
use lambda_http::{Request, Body};
use uuid::Uuid;
use std::time::SystemTime;

#[derive(Serialize, Deserialize)]
pub struct RequestEntity {
    pub id: String,
    pub value: String,
    pub created_at: DateTime<Utc>,
    pub created_by: String,
    // updated_at: Option<DateTime<Utc>>,
    // updated_by: Option<String>,
}

#[derive(Serialize, Deserialize)]
pub struct RequestDto {
    pub value: String,
}

impl TryInto<RequestDto> for Request {
    type Error = LambdaError;

    fn try_into(self) -> Result<RequestDto, LambdaError> {
        let body = match self.body() {
            Body::Text(t) => Ok(t),
            _ => Err(LambdaError::new(400, "could not parse payload".to_string()))
        }?;

        serde_json::from_str(body)
            .map_err(|err| LambdaError::new(400, format!("unable to parse json: {:?}", err)))
    }
}

impl RequestEntity {
    pub fn create(dto: RequestDto, username: String) -> Self {
        RequestEntity {
            id: Uuid::new_v4().to_string(),
            value: dto.value,
            created_at: SystemTime::now().into(),
            created_by: username
        }
    }
}

impl From<HashMap<String, AttributeValue>> for RequestEntity {
    fn from(map: HashMap<String, AttributeValue>) -> Self {
        RequestEntity {
            id: map.get("id").unwrap().s.as_ref().unwrap().to_string(),
            value: map.get("value").unwrap().s.as_ref().unwrap().to_string(),
            created_at: map.get("created_at").map(|value| value.s.as_ref()).unwrap().unwrap().parse().unwrap(),
            created_by: map.get("created_by").map(|value| value.s.as_ref()).unwrap().unwrap().parse().unwrap(),
        }
    }
}

impl Into<HashMap<String,AttributeValue>> for RequestEntity {
    fn into(self) -> HashMap<String,AttributeValue> {
        let mut map: HashMap<String, AttributeValue> = HashMap::new();
        map.add("id", self.id);
        map.add("value", self.value);
        map.add("created_at", self.created_at.to_string());
        map.add("created_by", self.created_by.to_string());
        map
    }
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