package edu.baylor.flarn.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@SequenceGenerator(name="sequence", initialValue=1, allocationSize=1)
public class ProblemSet {
    @NotNull
    String title;

    @NotNull
    String description;

    @NotNull
    @OneToOne
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    KnowledgeSource knowledgeSource;

    @OneToMany
    Set<Question> question = new HashSet<>();

    @NotNull
    Difficulty difficulty;

    @OneToMany
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    Set<Review> reviews = new HashSet<>();

    @ManyToOne
    //  @NotNull TODO: Add this later
    @JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    User moderator;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="sequence")
    private Long id;

    @ManyToOne
    private Category category;
}
