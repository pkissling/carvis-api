use serde::{Serialize, Deserialize};
use lambda_http::{IntoResponse,Response,Body};
use lambda_http::http::StatusCode;
use std::error::Error;
use std::fmt;

#[derive(Serialize, Deserialize, Debug)]
pub struct CarvisError {
    pub status_code: u16,
    pub message: String,
}

impl CarvisError {
    pub fn new(status_code: u16, message: String) -> Self {
        CarvisError {
            status_code,
            message: message.to_string() }
    }
}

unsafe impl Send for CarvisError {}
unsafe impl Sync for CarvisError {}
impl Error for CarvisError {}

impl fmt::Display for CarvisError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "TODO!")
    }
}
impl IntoResponse for CarvisError {
    fn into_response(self) -> Response<Body> {
        Response::builder()
            .status(StatusCode::from_u16(self.status_code).unwrap_or(StatusCode::INTERNAL_SERVER_ERROR))
            .body(serde_json::to_string(&self.message).unwrap())
            .expect("failed to render response")
            .into_response()
    }
}