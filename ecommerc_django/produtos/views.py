from django.views import View
from django.http import JsonResponse
from django.shortcuts import render
from rest_framework.generics import ListAPIView
from .serializers import ProductSerializer
from .models import Product
from .forms import ProductForm


def home(request):
    return render(request, 'home.html')


class ProductView(ListAPIView):
    queryset = Product.objects.filter(active=True)
    serializer_class = ProductSerializer


class ProductFormView(View):
    def get(self, request, *args, **kwargs):
        form = ProductForm()
        return render(request, 'form.html', {'form': form})

    def post(self, request, *args, **kwargs):
        form = ProductForm(request.POST)
        if form.is_valid():
            form.save()
            return JsonResponse({'valid': True})
        else:
            return JsonResponse({'valid': False, 'errors': form.errors})