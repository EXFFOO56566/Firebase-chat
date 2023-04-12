<?php

namespace App\Models;

use App\Models\Base\Role as BaseRole;

class Role extends BaseRole
{
	protected $fillable = [
		'name',
		'display_name',
		'description'
	];
}
