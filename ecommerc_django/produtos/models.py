from django.db import models

class Category(models.Model):
    name = models.CharField(("nome da categoria"), max_length=50)

    class Meta:
        verbose_name = "Categoria"
        verbose_name_plural = "Categorias"

    def __str__(self):
        return self.name

class Product(models.Model):
    product_name = models.CharField(("nome do produto"), max_length=50, unique=True)
    description = models.TextField(("Descrição do produto"), max_length=2000)
    created_at = models.DateTimeField(auto_now_add=True)
    category = models.ForeignKey(Category, on_delete=models.SET_NULL, null=True, blank=True, related_name="products")
    storage = models.PositiveIntegerField(default=0)
    active = models.BooleanField(default=True)

    class Meta:
        ordering = ["product_name"]
        verbose_name = "Produto"
        verbose_name_plural = "Produtos"

    def __str__(self):
        return self.product_name
