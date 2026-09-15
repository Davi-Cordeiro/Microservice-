from django.contrib import admin
from .models import Product

@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ["product_name", "description", "active", "category", "storage"]
    ordering=["product_name"]
    search_fields = ["product_name"]
    readonly_fields = ["created_at"]
    fieldsets = (
            ("Informações principais", {
            "fields": ["product_name", "description"]
            }),
            ("Estoque e categoria", {
                "fields": ["category", "storage"]
            }),
            ("Informações secundárias", {
                "fields": ["active", "created_at"]
            }),
        )
