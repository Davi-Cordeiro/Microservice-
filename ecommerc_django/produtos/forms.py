from django import forms
from django.forms import ModelForm
from .models import Product


class ProductForm(ModelForm):
    class Meta:
        model = Product
        exclude = ('active',)
        labels = {
            'product_name': 'Nome do Produto',
            'description': 'Descrição do Produto',
            'category': 'Categoria',
            'storage': 'Quantidade do produto em estoque'
        }
        widgets = {
            'product_name': forms.TextInput(attrs={
                'class': ' pl-3  text-black group rounded-sm bg-slate-400 border-2 border-slate-700'
            }),
            'description': forms.Textarea(attrs={
                'class': ' pl-3 text-black group rounded-sm bg-slate-400 border-2 border-slate-700'
            }),
            'category': forms.Select(attrs={
                'class': ' pl-3 text-black group rounded-sm bg-slate-400 border-2 border-slate-700'
            }),
            'storage': forms.NumberInput(attrs={
                'class': ' pl-3 text-black group rounded-sm bg-slate-400 border-2 border-slate-700'
            }),
        }
