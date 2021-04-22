FROM rust:1-slim-buster AS cargo-builder
WORKDIR /code
COPY src/ /code/src/
COPY Cargo.toml /code/Cargo.toml
COPY Cargo.lock /code/Cargo.lock
RUN rustup target add x86_64-unknown-linux-musl
RUN cargo build --release --target x86_64-unknown-linux-musl

FROM alpine:3.13
COPY --from=cargo-builder /code/target/x86_64-unknown-linux-musl/release/cars-get /usr/local/bin/bootstrap
WORKDIR /
ENTRYPOINT ["bootstrap"]