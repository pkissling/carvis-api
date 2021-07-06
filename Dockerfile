FROM ekidd/rust-musl-builder:latest AS cargo-builder
ARG FUNCTION_NAME
ADD --chown=rust:rust . ./
RUN cargo build --release  --manifest-path ./lambdas/$FUNCTION_NAME/Cargo.toml

FROM alpine:3.13
ARG FUNCTION_NAME
RUN apk --no-cache add ca-certificates
COPY --from=cargo-builder /home/rust/src/lambdas/$FUNCTION_NAME/target/x86_64-unknown-linux-musl/release/$FUNCTION_NAME /usr/local/bin/bootstrap
WORKDIR /
ENTRYPOINT ["bootstrap"]