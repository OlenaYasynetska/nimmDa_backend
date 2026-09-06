package com.nimmda.infrastructure.user;

import com.nimmda.domain.shared.UserId;
import com.nimmda.domain.user.AccountMode;
import com.nimmda.domain.user.User;
import com.nimmda.domain.user.UserRepository;
import com.nimmda.domain.user.UserRole;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryJpaAdapter implements UserRepository {

    private final SpringDataUserJpaRepository jpaRepository;

    public UserRepositoryJpaAdapter(SpringDataUserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        return toDomain(jpaRepository.save(toEntity(user)));
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    private UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.id().value());
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setFirstName(user.firstName());
        entity.setLastName(user.lastName());
        entity.setRole(UserJpaRole.valueOf(user.role().name()));
        entity.setAccountMode(UserJpaAccountMode.valueOf(user.accountMode().name()));
        entity.setEmailVerified(user.emailVerified());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        return User.rehydrate(
                new UserId(entity.getId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFirstName(),
                entity.getLastName(),
                UserRole.valueOf(entity.getRole().name()),
                AccountMode.valueOf(entity.getAccountMode().name()),
                entity.isEmailVerified(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
