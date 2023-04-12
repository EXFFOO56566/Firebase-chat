<?php

namespace App\Menu;

use Illuminate\Support\Facades\Auth;
use JeroenNoten\LaravelAdminLte\Menu\Builder;
use JeroenNoten\LaravelAdminLte\Menu\Filters\FilterInterface;
use Laratrust;

class MenuFilter implements FilterInterface
{
    public function transform($item, Builder $builder)
    {
        // base on user role
        if (isset($item['role']) && !Auth::user()->hasRole($item['role'])) {
            return false;
        }

        return $item;

    }
}
