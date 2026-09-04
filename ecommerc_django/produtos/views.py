from django.shortcuts import render
from rest_framework.response import Response
from rest_framework.views import APIView
from .serializers import ProductSerializer
from .models import Product


def home(request):
    return render(request, 'home.html')


class ProductView(APIView):
    queryset = Product.objects.all()

    def get(self, request):
        produtos = self.queryset

        serializer = ProductSerializer(produtos, many=True)

        return Response(serializer.data)
