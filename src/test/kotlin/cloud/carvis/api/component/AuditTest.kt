package cloud.carvis.api.component

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.cars.dao.CarRepository
import cloud.carvis.api.cars.model.CarEntity
import cloud.carvis.api.common.dao.BaseRepository
import cloud.carvis.api.common.dao.model.Entity
import cloud.carvis.api.requests.dao.RequestRepository
import cloud.carvis.api.requests.model.entities.RequestEntity
import cloud.carvis.api.shareableLinks.dao.ShareableLinkRepository
import cloud.carvis.api.shareableLinks.model.ShareableLinkEntity
import cloud.carvis.api.users.dao.NewUserRepository
import cloud.carvis.api.users.model.NewUserEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant


class AuditTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var carRepository: CarRepository

    @Autowired
    private lateinit var requestRepository: RequestRepository

    @Autowired
    private lateinit var shareableLinkRepository: ShareableLinkRepository

    @Autowired
    private lateinit var newUserRepository: NewUserRepository

    @Test
    fun `CarEntity - createdAt, createdBy`() {
        testCreateAtCreatedBy(carRepository) { CarEntity() }
    }

    @Test
    fun `CarEntity - updatedAt, updatedBy`() {
        testUpdatedAtUpdatedBy(carRepository) { CarEntity() }
    }

    @Test
    fun `RequestEntity - createdAt, createdBy`() {
        testCreateAtCreatedBy(requestRepository) { RequestEntity() }
    }

    @Test
    fun `RequestEntity - updatedAt, updatedBy`() {
        testUpdatedAtUpdatedBy(requestRepository) { RequestEntity() }
    }

    @Test
    fun `ShareableLinkEntity - createdAt, createdBy`() {
        testCreateAtCreatedBy(shareableLinkRepository) { ShareableLinkEntity() }
    }

    @Test
    fun `ShareableLinkEntity - updatedAt, updatedBy`() {
        testUpdatedAtUpdatedBy(shareableLinkRepository) { ShareableLinkEntity() }
    }

    @Test
    fun `NewUserEntity - createdAt, createdBy`() {
        testCreateAtCreatedBy(newUserRepository) { NewUserEntity() }
    }

    @Test
    fun `NewUserEntity - updatedAt, updatedBy`() {
        testUpdatedAtUpdatedBy(newUserRepository) { NewUserEntity() }
    }

    fun <T : Entity<*>, K : Any> testCreateAtCreatedBy(repository: BaseRepository<T, K>, entityCtor: () -> T) {
        // given
        val start = Instant.now()
        val entity = entityCtor.invoke()
        assertThat(entity.createdAt).isNull()
        assertThat(entity.createdBy).isNull()

        // when
        val savedEntity = repository.save(entity)

        // then
        assertThat(savedEntity.createdAt).isBetween(start, Instant.now())
        assertThat(savedEntity.createdBy).isNull()
        assertThat(savedEntity.updatedAt).isEqualTo(savedEntity.createdAt)
        assertThat(savedEntity.updatedBy).isNull()
    }

    fun <T : Entity<*>, K : Any> testUpdatedAtUpdatedBy(repository: BaseRepository<T, K>, entityCtor: () -> T) {
        // given
        val start = Instant.now()
        val entity = entityCtor.invoke()
        val savedEntity = repository.save(entity)
        assertThat(savedEntity.createdAt).isBetween(start, Instant.now())
        assertThat(savedEntity.updatedAt).isEqualTo(savedEntity.createdAt)

        // when
        val updatedEntity = repository.save(savedEntity)

        // then
        assertThat(updatedEntity.createdAt).isEqualTo(savedEntity.createdAt)
        assertThat(updatedEntity.updatedAt).isAfter(savedEntity.createdAt)
        assertThat(updatedEntity.updatedBy).isNull()
        assertThat(updatedEntity.createdAt).isNotEqualTo(savedEntity.updatedAt)
    }
}
