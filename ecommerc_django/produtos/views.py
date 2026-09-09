from django.shortcuts import render
from rest_framework.generics import ListCreateAPIView
from .serializers import ProductSerializer
from .models import Product


def home(request):
    return render(request, 'produtos/home.html')


class ProductView(ListCreateAPIView):
    queryset = Product.objects.filter(active=True)
    serializer_class = ProductSerializer