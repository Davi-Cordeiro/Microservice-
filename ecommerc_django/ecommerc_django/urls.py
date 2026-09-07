
from django.contrib import admin
from django.urls import path
from django.urls import include
from produtos.views import home

urlpatterns = [
    path('', home, name='home'),
    path('admin/', admin.site.urls),
    path('produto/', include('produtos.urls'))
]
