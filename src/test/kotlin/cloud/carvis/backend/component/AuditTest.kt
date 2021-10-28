package cloud.carvis.backend.integration

import cloud.carvis.backend.dao.repositories.CarRepository
import cloud.carvis.backend.model.entities.CarEntity
import cloud.carvis.backend.util.AmazonDynamoDbTestConfig
import cloud.carvis.backend.util.AmazonS3TestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Instant


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.data.dynamodb.entity2ddl.auto=create-only",
        "spring.main.allow-bean-definition-overriding=true"
    ],
    classes = [
        AmazonS3TestConfig::class,
        AmazonDynamoDbTestConfig::class
    ]
)
class AuditTest {

    @Autowired
    private lateinit var carRepository: CarRepository

    @Test
    fun `CarsEntity - createdAt, createdBy`() {
        // given
        val start = Instant.now()
        val carEntity = CarEntity()
        assertThat(carEntity.createdAt).isNull()
        assertThat(carEntity.ownerUsername).isNull()

        // when
        val savedCar = carRepository.save(carEntity)

        // then
        assertThat(savedCar.createdAt).isBetween(start, Instant.now())
        assertThat(savedCar.ownerUsername).isNull()
        assertThat(savedCar.updatedAt).isEqualTo(savedCar.createdAt)
        assertThat(savedCar.lastModifiedBy).isNull()
    }

    @Test
    fun `CarsEntity - updatedAt, updatedBy`() {
        // given
        val start = Instant.now()
        val car = carRepository.save(CarEntity())
        assertThat(car.createdAt).isBetween(start, Instant.now())
        assertThat(car.updatedAt).isEqualTo(car.createdAt)

        // when
        car.brand = "Mercedes"
        val updatedCar = carRepository.save(car)

        // then
        assertThat(updatedCar.createdAt).isEqualTo(car.createdAt)
        assertThat(updatedCar.updatedAt).isAfter(car.createdAt)
        assertThat(updatedCar.lastModifiedBy).isNull()
        assertThat(updatedCar.createdAt).isNotEqualTo(car.updatedAt)
    }

}
