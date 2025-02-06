package xyz.jimh.souschef.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "categories")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class Category(
    var name: String,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null)