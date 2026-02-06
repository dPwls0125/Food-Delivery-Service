package personal.yejin.foodDelivery.domain.common;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter // For setId()
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For SuperBuilder
@MappedSuperclass
public abstract class GlobalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id; // Make it protected so child classes can access in builder

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
