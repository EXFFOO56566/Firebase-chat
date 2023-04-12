<?php

return [

    /*
    |--------------------------------------------------------------------------
    | Theme Demo
    |--------------------------------------------------------------------------
    | Demo Theme v3.x here:
    | https://adminlte.io/themes/dev/AdminLTE/index.html
    |
    */

    /*
    |--------------------------------------------------------------------------
    | Title
    |--------------------------------------------------------------------------
    |
    | Here you can change the default title of your admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#61-title
    |
    */

    'title' => 'Attendance Fingerprint',
    'title_prefix' => '',
    'title_postfix' => '',

    /*
    |--------------------------------------------------------------------------
    | Favicon
    |--------------------------------------------------------------------------
    |
    | Here you can activate the favicon.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#62-favicon
    |
    */

    'use_ico_only' => false,
    'use_full_favicon' => true,

    /*
    |--------------------------------------------------------------------------
    | Logo
    |--------------------------------------------------------------------------
    |
    | Here you can change the logo of your admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#63-logo
    |
    */

    'logo' => '<b>Attendance</b>FR',
    'logo_img' => 'logo/logo.png',
    'logo_img_class' => 'brand-image img-circle elevation-3',
    'logo_img_xl' => null,
    'logo_img_xl_class' => 'brand-image-xs',
    'logo_img_alt' => 'MuliaTech',

    /*
    |--------------------------------------------------------------------------
    | User Menu
    |--------------------------------------------------------------------------
    |
    | Here you can activate and change the user menu.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#64-user-menu
    |
    */

    'usermenu_enabled' => true,
    'usermenu_header' => true,
    'usermenu_header_class' => '',
    'usermenu_image' => true,
    'usermenu_desc' => true,

    /*
    |--------------------------------------------------------------------------
    | Layout
    |--------------------------------------------------------------------------
    |
    | Here we change the layout of your admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#65-layout
    |
    */

    'layout_topnav' => null,
    'layout_boxed' => null,
    'layout_fixed_sidebar' => true,
    'layout_fixed_navbar' => null,
    'layout_fixed_footer' => null,

    /*
    |--------------------------------------------------------------------------
    | Extra Classes
    |--------------------------------------------------------------------------
    |
    | Here you can change the look and behavior of the admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#66-classes
    | Set color doc : https://adminlte.io/docs/3.0/layout.html
    */

    'classes_body' => '', // ex: accent-purple
    'classes_brand' => 'bg-blue',
    'classes_brand_text' => '',
    'classes_content_header' => 'container-fluid',
    'classes_content' => 'container-fluid',
    'classes_sidebar' => 'sidebar-light-blue elevation-4', // can use light or dark
    'classes_sidebar_nav' => '',
    'classes_topnav' => 'navbar-blue navbar-dark',
    'classes_topnav_nav' => 'navbar-expand-md',
    'classes_topnav_container' => 'container',

    /*
    |--------------------------------------------------------------------------
    | Sidebar
    |--------------------------------------------------------------------------
    |
    | Here we can modify the sidebar of the admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#67-sidebar
    |
    */

    'sidebar_mini' => true,
    'sidebar_collapse' => false,
    'sidebar_collapse_auto_size' => false,
    'sidebar_collapse_remember' => false,
    'sidebar_collapse_remember_no_transition' => true,
    'sidebar_scrollbar_theme' => 'os-theme-light',
    'sidebar_scrollbar_auto_hide' => 'l',
    'sidebar_nav_accordion' => true,
    'sidebar_nav_animation_speed' => 300,

    /*
    |--------------------------------------------------------------------------
    | Control Sidebar (Right Sidebar)
    |--------------------------------------------------------------------------
    |
    | Here we can modify the right sidebar aka control sidebar of the admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#68-control-sidebar-right-sidebar
    |
    */

    'right_sidebar' => false,
    'right_sidebar_icon' => 'fas fa-cogs',
    'right_sidebar_theme' => 'dark',
    'right_sidebar_slide' => true,
    'right_sidebar_push' => true,
    'right_sidebar_scrollbar_theme' => 'os-theme-light',
    'right_sidebar_scrollbar_auto_hide' => 'l',

    /*
    |--------------------------------------------------------------------------
    | URLs
    |--------------------------------------------------------------------------
    |
    | Here we can modify the url settings of the admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#69-urls
    |
    */

    'use_route_url' => false,

    'dashboard_url' => 'home',

    'logout_url' => 'logout',

    'login_url' => 'login',

    'register_url' => 'register',

    'password_reset_url' => 'password/reset',

    'password_email_url' => 'password/email',

    'profile_url' => false,

    /*
    |--------------------------------------------------------------------------
    | Laravel Mix
    |--------------------------------------------------------------------------
    |
    | Here we can enable the Laravel Mix option for the admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#610-laravel-mix
    |
    */

    'enabled_laravel_mix' => false,

    /*
    |--------------------------------------------------------------------------
    | Menu Items
    |--------------------------------------------------------------------------
    |
    | Here we can modify the sidebar/top navigation of the admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#611-menu
    |
    | Example use :
    | ['header' => 'labels'], // Header of menu
    | [
    |   'text' => 'search', // Text menu
    |   'search' => true, // Search enable
    |   'topnav' => true, // Menu will place on top
    |   'role' => 'administrator', // Role user access
    |   'url'  => 'admin/blog', // Url of the menu
    |   'can'  => 'manage-blog', // Permission can edit or not
    |   'icon' => 'far fa-fw fa-file', // Icon
    |   'icon_color' => 'red', // Icon color
    |   'label' => 4, // Add label like notification, will show as [4]
    |   'label_color' => 'success', // Label color
    |   'submenu' => [] // Submenu
    | ]
    |
    */

    /*
    | Template :
    | Copy this template to create new menu when generate CRUD
    | Change PostMenu -> the name of menu
    |
    |
       [
            'text' => 'PostMenu',
            'icon' => 'fa fa-PostMenu',
            'submenu' => [
                [
                    'text' => 'List',
                    'url'  => 'PostMenu',
                    'icon' => 'fa fa-list-ul',
                ],
                [
                    'text' => 'Add or Update',
                    'url'  => 'PostMenu/add',
                    'icon' => 'fa fa-edit',
                ],
            ],
            'role' => '',
        ],
    |
    */

    'menu' => [
        // Top Nav
        [
            'search' => true,
            'url' => 'test',  //form action
            'method' => 'POST', //form method
            'input_name' => 'menu-search-input', //input name
            'text' => 'Search', //input placeholder
            'topnav' => true,
            'role' => 'administrator',
        ],
        [
            'text' => 'Dashboard',
            'url'  => 'home',
            'icon' => 'fa fa-tachometer-alt',
        ],
        [
            'text' => 'Attendance',
            'url'  => 'attendances',
            'icon' => 'fa fa-database',
            'role' => 'administrator|admin|staff',
        ],
        [
            'text' => 'Analytics',
            'url'  => 'analytics',
            'icon' => 'fa fa-chart-area',
            'role' => 'administrator',
        ],
        [
            'text' => 'Areas',
            'icon' => 'fa fa-map-marked-alt',
            'submenu' => [
                [
                    'text' => 'List',
                    'url'  => 'areas',
                    'icon' => 'fa fa-list-ul',
                ],
                [
                    'text' => 'Add or Update',
                    'url'  => 'areas/add',
                    'icon' => 'fa fa-edit',
                ],
            ],
            'role' => 'administrator',
        ],
        [
            'text' => 'Users',
            'icon' => 'fa fa-user',
            'submenu' => [
                [
                    'text' => 'List',
                    'url'  => 'users',
                    'icon' => 'fa fa-list-ul',
                ],
                [
                    'text' => 'Add or Update',
                    'url'  => 'users/add',
                    'icon' => 'fa fa-edit',
                ],
                [
                    'text' => 'Import CSV',
                    'url'  => 'users/import',
                    'icon' => 'fa fa-upload',
                ],
            ],
            'role' => 'administrator',
        ],
        [
            'text' => 'Settings',
            'url'  => 'settings',
            'icon' => 'fa fa-cogs',
            'role' => 'administrator',
        ],
        [
            'text' => 'Profile',
            'url'  => 'profile/details',
            'icon' => 'fa fa-id-card',
        ],
    ],

    /*
    |--------------------------------------------------------------------------
    | Menu Filters
    |--------------------------------------------------------------------------
    |
    | Here we can modify the menu filters of the admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#612-menu-filters
    |
    */

    'filters' => [
        JeroenNoten\LaravelAdminLte\Menu\Filters\HrefFilter::class,
        JeroenNoten\LaravelAdminLte\Menu\Filters\SearchFilter::class,
        JeroenNoten\LaravelAdminLte\Menu\Filters\ActiveFilter::class,
        JeroenNoten\LaravelAdminLte\Menu\Filters\SubmenuFilter::class,
        JeroenNoten\LaravelAdminLte\Menu\Filters\ClassesFilter::class,
        // JeroenNoten\LaravelAdminLte\Menu\Filters\GateFilter::class, // Remove gate
        App\Menu\MenuFilter::class, // Custom menu for role
        JeroenNoten\LaravelAdminLte\Menu\Filters\LangFilter::class,
    ],

    /*
    |--------------------------------------------------------------------------
    | Plugins Initialization
    |--------------------------------------------------------------------------
    |
    | Here we can modify the plugins used inside the admin panel.
    |
    | For more detailed instructions you can look here:
    | https://github.com/jeroennoten/Laravel-AdminLTE/#613-plugins
    |
    */

    'plugins' => [
        [
            'name' => 'Datatables',
            'active' => true,
            'files' => [
                [
                    'type' => 'js',
                    'asset' => true,
                    'location' => app()->runningInConsole() ? '' : asset('vendor/datatables/js/jquery.dataTables.js'),
                ],
                [
                    'type' => 'js',
                    'asset' => true,
                    'location' => app()->runningInConsole() ? '' : asset('vendor/datatables/js/dataTables.bootstrap4.js'),
                ],
                [
                    'type' => 'css',
                    'asset' => true,
                    'location' => app()->runningInConsole() ? '' : asset('vendor/datatables/css/dataTables.bootstrap4.css'),
                ],
            ],
        ],
        [
            'name' => 'Select2',
            'active' => true,
            'files' => [
                [
                    'type' => 'js',
                    'asset' => true,
                    'location' => app()->runningInConsole() ? '' : asset('vendor/select2/js/select2.js'),
                ],
                [
                    'type' => 'css',
                    'asset' => true,
                    'location' => app()->runningInConsole() ? '' : asset('vendor/select2/css/select2.css'),
                ],
            ],
        ],
        [
            'name' => 'Chartjs',
            'active' => true,
            'files' => [
                [
                    'type' => 'js',
                    'asset' => true,
                    'location' => app()->runningInConsole() ? '' : asset('vendor/chart.js/Chart.js'),
                ],
            ],
        ],
        [
            'name' => 'Sweetalert2',
            'active' => true,
            'files' => [
                [
                    'type' => 'js',
                    'asset' => true,
                    'location' => app()->runningInConsole() ? '' : asset('vendor/sweetalert2/sweetalert2.all.js'),
                ],
            ],
        ],
        [
            'name' => 'Pace',
            'active' => false,
            'files' => [
                [
                    'type' => 'css',
                    'asset' => false,
                    'location' => '//cdnjs.cloudflare.com/ajax/libs/pace/1.0.2/themes/blue/pace-theme-center-radar.min.css',
                ],
                [
                    'type' => 'js',
                    'asset' => false,
                    'location' => '//cdnjs.cloudflare.com/ajax/libs/pace/1.0.2/pace.min.js',
                ],
            ],
        ],
    ],
];
