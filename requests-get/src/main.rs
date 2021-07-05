use lambda_http::{handler, lambda_runtime::{self, Context}, IntoResponse, Request, Response, RequestExt};
use rusoto_dynamodb::{DynamoDb, DynamoDbClient, GetItemInput, AttributeValue};
use rusoto_core::Region;
use std::env;
use std::collections::HashMap;
use http::StatusCode;
use alcoholic_jwt::{token_kid, validate, Validation, JWKS, ValidationError};

type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

struct User {
    username: String,
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(func)).await?;
    Ok(())
}

async fn func(req: Request, _: Context) -> Result<impl IntoResponse, Error> {
    let request_id = req.path_parameters().get("requestId").expect("requestId not present").to_string();
    let user = auth(&req).expect("Unable to auth");

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
        Ok(_) => response(StatusCode::OK, user.username.as_str()),
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
        Validation::SubjectPresent
    ];
    let token = req.headers().get("Authorization").map_or("", |header| header.to_str().expect("bla"));
    let kid = token_kid(token)
        .expect("Failed to decode token headers")
        .expect("No 'kid' claim present in token");

    let jwk = jwks.find(&kid).expect("Specified key not found in set");
    let claims = validate(token, jwk, validations)?.claims;

    Ok(User {
        username: claims.get("sub").unwrap().as_str().unwrap().to_string(),
    })
}

fn fetch_jwks(uri: &str) -> Result<JWKS, Error> {
    let response = reqwest::blocking::get(uri)?;
    let val = response.json::<JWKS>()?;
    return Ok(val);
}