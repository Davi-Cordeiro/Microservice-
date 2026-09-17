from django import forms
from django.forms import ModelForm
from .models import Product


class ProductForm(ModelForm):
    class Meta:
        model = Product
        exclude = ('active',)
        widgets = {
            'product_name': forms.TextInput(attrs={
                'class': ' pl-3 group rounded-sm bg-slate-400 border-2 border-slate-700'
            }),
            'description': forms.Textarea(attrs={
                'class': ' pl-3 text-black'
            }),
            'category': forms.Select(attrs={
                'class': ' pl-3 text-black'
            }),
            'storage': forms.NumberInput(attrs={
                'class': ' pl-3 text-black'
            }),
        }
