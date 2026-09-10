from django import views
from django.http import JsonResponse, request
from django.shortcuts import render
from rest_framework.generics import ListAPIView
from .serializers import ProductSerializer
from .models import Product
from django.forms import ModelForm


def home(request):
    return render(request, 'home.html')


class ProductView(ListAPIView):
    queryset = Product.objects.filter(active=True)
    serializer_class = ProductSerializer

class ProductForm(ModelForm):
    class Meta:
        model = Product
        exclude = ('active',)

class ValidadeProductForm(views.View):
    def post(self, request, *args, **kwargs):
        form = ProductForm(request.POST)
        if form.is_valid():
            form.save()
            return JsonResponse({'valid': True})
        else:
            return JsonResponse({'valid': False, 'errors': form.errors})

    def get(self, request, *args, **kwargs):
        form = ProductForm()
        return render(request, 'product_form.html', {'form': form})