use std::collections::HashMap;
use rusoto_dynamodb::AttributeValue;

trait Add {
    fn add(&mut self, key: &str, value: String);
}

impl Add for HashMap<String, AttributeValue> {
    fn add(&mut self, key: &str, value: String) {
        self.insert(key.to_string(), AttributeValue {
            s: Some(value),
            ..Default::default()
        });
    }
}