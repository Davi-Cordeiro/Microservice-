
from django.urls import path
from django.urls import include
from .views import ProductView

urlpatterns = [
    path('api-auth/', include('rest_framework.urls')),
    path('api/', ProductView.as_view(), name="produtos_api")
]
