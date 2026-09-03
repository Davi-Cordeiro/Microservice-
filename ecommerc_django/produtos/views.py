from django.shortcuts import render
from rest_framework.response import Response
from rest_framework.views import APIView
from .models import Product


def home(request):
    return render(request, 'ecommerc_django/produtos/templates/home.html')


class ProductSerializer(APIView):
    def get(self, response):
        produtos = Product.objects.filter.all()
        serializer = ProductSerializer(produtos)

        return Response(serializer.data)
