package main

import (
	"mpm-go/model"
	"net/http"

	"github.com/gin-gonic/gin"
)

func getActivitiesApi(c *gin.Context) {
	var as []model.TActivity

	db().Find(&as)
	c.JSON(http.StatusOK, Response{0, as})
}

/*

@PostMapping("/api/createOrUpdateActivity")
public int createOrUpdateActivity(@RequestBody ActivityParam param) {
	EntityActivity activity = param.activity;
	log.info("param {}", param);
	if (activity.getId() == null) {
		EntityActivity inserted = dao.insert(activity, true, false, false);
		EntityPhoto photo = dao.fetch(EntityPhoto.class, param.fromPhoto);
		if (photo != null) {
			// update photo
			photo.setActivity(inserted.getId());
			dao.update(photo);
		}
		return inserted.getId().intValue();
	} else {
		return dao.updateIgnoreNull(activity);
	}
}
*/
