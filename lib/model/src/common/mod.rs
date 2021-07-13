use serde::{Serialize, Deserialize};
use lambda_http::{IntoResponse,Response,Body};
use lambda_http::http::StatusCode;

#[derive(Serialize, Deserialize)]
pub struct LambdaError {
    pub status_code: u16,
    pub error: LambdaErrorPayload,
}

#[derive(Serialize, Deserialize)]
pub struct LambdaErrorPayload {
    pub message: String
}

impl LambdaError {
    pub fn new(status_code: u16, message: String) -> Self {
        LambdaError {
            status_code,
            error: LambdaErrorPayload { message }
        }
    }
}

#[derive(Serialize, Deserialize)]
pub struct LambdaResult {
    pub status_code: u16,
    pub message: String
}

impl LambdaResult {
    pub fn new(status_code: u16, message: String) -> Self {
        LambdaResult {
            status_code,
            message: message.to_string() }
    }
}

impl IntoResponse for LambdaError {
    fn into_response(self) -> Response<Body> {
        Response::builder()
            .status(StatusCode::from_u16(self.status_code).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR))
            .body(serde_json::to_string(&self.error).unwrap_or("error while creating json in error response".to_string()))
            .expect("failed to render response")
            .into_response()
    }
}

impl IntoResponse for LambdaResult {
    fn into_response(self) -> Response<Body> {
        Response::builder()
            .status(StatusCode::from_u16(self.status_code).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR))
            .body(serde_json::to_string(&self.message).unwrap_or("error while creating json in success response".to_string()))
            .expect("failed to render response")
            .into_response()
    }
}