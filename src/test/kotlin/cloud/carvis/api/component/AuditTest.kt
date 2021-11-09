package cloud.carvis.api.component

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.dao.repositories.CarRepository
import cloud.carvis.api.model.entities.CarEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant


class AuditTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var carRepository: CarRepository

    @Test
    fun `CarsEntity - createdAt, createdBy`() {
        // given
        val start = Instant.now()
        val carEntity = CarEntity()
        assertThat(carEntity.createdAt).isNull()
        assertThat(carEntity.createdBy).isNull()

        // when
        val savedCar = carRepository.save(carEntity)

        // then
        assertThat(savedCar.createdAt).isBetween(start, Instant.now())
        assertThat(savedCar.createdBy).isNull()
        assertThat(savedCar.updatedAt).isEqualTo(savedCar.createdAt)
        assertThat(savedCar.updatedBy).isNull()
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
        assertThat(updatedCar.updatedBy).isNull()
        assertThat(updatedCar.createdAt).isNotEqualTo(car.updatedAt)
    }

}
