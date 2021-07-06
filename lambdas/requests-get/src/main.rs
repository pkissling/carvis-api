use lambda_http::{handler, lambda_runtime::{self, Context}, IntoResponse, Request, Response, RequestExt};
use rusoto_dynamodb::{DynamoDb, DynamoDbClient, GetItemInput, AttributeValue};
use rusoto_core::Region;
use std::env;
use std::collections::HashMap;
use http::{StatusCode, HeaderValue, HeaderMap};
use alcoholic_jwt::{token_kid, validate, Validation, JWKS, ValidationError, ValidJWT};
use serde_json::Value;
use serde::{Serialize, Deserialize};


type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

#[derive(Serialize, Deserialize)]
struct User {
    username: String,
    roles: Vec<String>,
}

#[derive(Serialize, Deserialize)]
struct ErrorResponse {
    message: String,
}

impl From<ValidJWT> for User {
    fn from(jwt: ValidJWT) -> Self {
        User {
            username: jwt.claims.get("sub").unwrap().as_str().unwrap().to_string(),
            roles: to_roles(jwt.claims),
        }
    }
}

fn to_roles(claims: Value) -> Vec<String> {
    let roles = claims.get("https://carvis.cloud/roles");
    if roles.is_none() {
        return Vec::new();
    }

    let roles = roles.unwrap();
    let roles_array = roles.as_array();
    if roles_array.is_none() {
        return Vec::new();
    }

    roles_array.unwrap().iter()
        .map(|v| v.as_str())
        .filter(|x| x.is_some())
        .map(|x| x.unwrap())
        .map(|x| x.to_string())
        .collect()
}


#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(func)).await?;
    Ok(())
}

async fn func(req: Request, _: Context) -> Result<impl IntoResponse, Error> {
    let request_id = req.path_parameters().get("requestId").expect("requestId not present").to_string();
    let auth_result = auth(&req);
    if auth_result.is_err() {
        return response(StatusCode::UNAUTHORIZED, format!("{:?}", auth_result.err().unwrap()).as_str());
    }

    let user = auth_result.unwrap();

    let client = DynamoDbClient::new(Region::EuWest1);
    let mut key_map = HashMap::new();
    key_map.insert("id".to_string(), AttributeValue {
        s: Some(request_id.to_string()),
        ..Default::default()
    });
    let input = GetItemInput {
        key: key_map,
        table_name: env::var("DYNAMODB_REQUESTS_TABLE_NAME").expect("env var 'DYNAMODB_REQUESTS_TABLE_NAME' not set"),
        ..Default::default()
    };

    match client.get_item(input).await {
        // Ok(output) => response(StatusCode::OK, output.item.unwrap().get("created_at").unwrap().s.as_ref().expect("unable to unwrap output")),
        Ok(_) => response(StatusCode::OK, serde_json::to_string(&user).expect("unable to create json from user struct").as_str()),
        Err(err) => response(StatusCode::INTERNAL_SERVER_ERROR, err.to_string().as_str())
    }
}

fn response(status_code: StatusCode, message: &str) -> Result<impl IntoResponse, Error> {
    Ok(Response::builder()
        .status(status_code)
        .body(message.to_string())
        .expect("failed to render response"))
}

fn auth(req: &Request) -> Result<User, ValidationError> {
    let authority = std::env::var("AUTHORITY").unwrap_or("https://carvis.eu.auth0.com/".to_string()); // TODO
    let jwks = fetch_jwks(&format!("{}{}", authority, ".well-known/jwks.json"))
        .expect("failed to fetch jwks");
    let validations = vec![
        Validation::Issuer(authority),
        Validation::SubjectPresent,
        Validation::NotExpired
    ];
    let token = extract_token(req.headers());
    println!("token: {}", token);
    let kid = token_kid(token)
        .expect("Failed to decode token headers")
        .expect("No 'kid' claim present in token");

    let jwk = jwks.find(&kid).expect("Specified key not found in set");
    match validate(token, jwk, validations) {
        Ok(jwt) => Ok(User::from(jwt)),
        Err(err) => Err(err)
    }
}

fn fetch_jwks(uri: &str) -> Result<JWKS, Error> {
    let response = reqwest::blocking::get(uri)?;
    let val = response.json::<JWKS>()?;
    return Ok(val);
}

fn extract_token(headers: &HeaderMap<HeaderValue>) -> &str {
    let auth_header = headers.get("Authorization").expect("No Authorization header provided.");
    let auth_header_str = auth_header.to_str().expect("Unable to extract value from Authorization header");
    if auth_header_str.to_lowercase().starts_with("bearer ") {
        auth_header_str[7..].as_ref()
    } else {
        auth_header_str
    }
}