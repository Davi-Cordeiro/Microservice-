from django.shortcuts import render
from rest_framework.generics import ListAPIView
from .serializers import ProductSerializer
from .models import Product


def home(request):
    return render(request, 'home.html')


class ProductView(ListAPIView):
    queryset = Product.objects.filter(active=True)
    serializer_class = ProductSerializer