use serde::{Serialize,Deserialize};

#[derive(Serialize, Deserialize)]
pub struct FooError {
    pub message: String
}
