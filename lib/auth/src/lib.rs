use alcoholic_jwt::{token_kid, validate, Validation, JWKS, ValidJWT};
use serde::{Serialize, Deserialize};
use serde_json::Value;
use http::{HeaderValue, HeaderMap};
use lambda_http::{Request};

extern crate model;

#[derive(Serialize, Deserialize)]
pub struct AuthError {
    pub status_code: u16,
    pub message: String
}

impl AuthError {
    fn new(status_code: u16, message: String) -> Self {
        AuthError { status_code, message }
    }
}
pub fn authenticate(request: &Request, authority: String, audience: String) -> Result<User, AuthError> {
    let jwks = fetch_jwks(&format!("{}{}", authority, ".well-known/jwks.json"))?;

    let validations = vec![
        Validation::Issuer(authority),
        Validation::SubjectPresent,
        Validation::Audience(audience),
        Validation::NotExpired
    ];

    let token = extract_token(request.headers())?;
    let kid = token_kid(token)
        .map_err(|err| AuthError::new(500, format!("unable to parse jwt: {:?}", err)))?
        .ok_or(AuthError::new(500, "unable to parse jwt".to_string()))?;

    let jwk = jwks.find(kid.as_str())
        .ok_or(AuthError::new(500, "Specified key not found in set".to_string()))?;

    match validate(token, jwk, validations) {
        Ok(jwt) => Ok(User::from(jwt)),
        Err(err) => Err(AuthError::new(401, format!("{:?}", err)))
    }
}

fn fetch_jwks(uri: &str) -> Result<JWKS, AuthError> {
    let jwks = reqwest::blocking::get(uri)
        .map_err(|err| AuthError::new(500, format!("unable to send http request: {}", err)))?
        .json::<JWKS>()
        .map_err(|err| AuthError::new(500, format!("unable to parse response: {}", err)))?;
    return Ok(jwks);
}

fn extract_token(headers: &HeaderMap<HeaderValue>) -> Result<&str, AuthError> {
    let auth_header = headers.get("Authorization")
        .ok_or(AuthError::new(401, "no 'Authorization' header present".to_string()))?
        .to_str()
        .map_err(|err| AuthError::new(401, format!("unable to extract string from 'Authorization' header: {}", err)))?;

    if auth_header.to_lowercase().starts_with("bearer ") {
        Ok(auth_header[7..].as_ref())
    } else {
        Ok(auth_header)
    }
}

#[derive(Serialize, Deserialize)]
pub struct User {
    username: String,
    roles: Vec<String>,
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