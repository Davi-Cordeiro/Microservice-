from django.db import models


class Product(models.Model):
    product_name = models.CharField(("nome do produto"), max_length=50)
    description = models.TextField(("Descrição do produto"), max_length=2000)

    def __str__(self):
        return self.product_name
