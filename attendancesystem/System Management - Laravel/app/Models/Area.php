<?php

namespace App\Models;

use App\Models\Base\Area as BaseArea;

class Area extends BaseArea
{
	protected $fillable = [
		'name',
		'address'
	];
}
