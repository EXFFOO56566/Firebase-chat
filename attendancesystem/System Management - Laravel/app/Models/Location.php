<?php

namespace App\Models;

use App\Models\Base\Location as BaseLocation;

class Location extends BaseLocation
{
	protected $fillable = [
		'lat',
		'longt',
        'area_id'
	];
}
