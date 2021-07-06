FROM ekidd/rust-musl-builder:latest  AS cargo-builder
ADD --chown=rust:rust . ./
RUN cargo build --release

FROM alpine:3.13
RUN apk --no-cache add ca-certificates
COPY --from=cargo-builder /home/rust/src/target/x86_64-unknown-linux-musl/release/cars-get /usr/local/bin/bootstrap
WORKDIR /
ENTRYPOINT ["bootstrap"]