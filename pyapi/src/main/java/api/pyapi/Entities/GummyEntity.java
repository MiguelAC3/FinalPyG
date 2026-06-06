package api.pyapi.Entities;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;


@Entity 
@Getter
@Setter
public class GummyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String flavor;
    private String color;
    private String shape;
    private List<String> ingredients;
    private int quantity;
    private double price;
    private boolean isVegan;

    @ManyToOne
    @JoinColumn(name = "entry_operator_id", nullable = false)
    @JsonBackReference("entry_operator_gummy")
    private UserEntity entryOperator;

    public GummyEntity() {}

}
