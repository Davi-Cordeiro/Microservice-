
from django.urls import path
from django.urls import include
from .views import ProductView, ValidadeProductForm

urlpatterns = [
    path('api-auth/', include('rest_framework.urls')),
    path('api/', ProductView.as_view(), name="produtos_api"),
    path('cadastro/', ValidadeProductForm.as_view(), name="Cadastro de novo produto")
]
