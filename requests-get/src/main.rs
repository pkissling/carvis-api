use lambda_http::{
    handler,
    lambda_runtime::{self, Context},
    IntoResponse, Request, Response,
};

type Error = Box<dyn std::error::Error + Send + Sync + 'static>;

#[tokio::main]
async fn main() -> Result<(), Error> {
    lambda_runtime::run(handler(func)).await?;
    Ok(())
}

async fn func(_: Request, _: Context) -> Result<impl IntoResponse, Error> {
    Ok(Response::builder()
            .status(200)
            .body("Hello, Test!".to_string())
            .expect("failed to render response"))
}
