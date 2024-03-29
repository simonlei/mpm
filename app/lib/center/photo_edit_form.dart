import 'package:app/main.dart';
import 'package:app/model/config.dart';
import 'package:app/model/pics_model.dart';
import 'package:app/widgets/tags_selector.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_form_builder/flutter_form_builder.dart';
import 'package:form_builder_validators/form_builder_validators.dart';
import 'package:intl/intl.dart';
import 'package:oktoast/oktoast.dart';

class PhotoEditForm extends StatefulWidget {
  final PicImage _image;

  PhotoEditForm(this._image);

  @override
  State<StatefulWidget> createState() {
    return _PhotoEditFormState();
  }
}

class _PhotoEditFormState extends State<PhotoEditForm> {
  final _formKey = GlobalKey<FormBuilderState>();

  @override
  Widget build(BuildContext context) {
    late TagsSelector tagsSelector;
    return Container(
      width: 250,
      child: Column(
        children: [
          Text('照片属性编辑'),
          FormBuilder(
            key: _formKey,
            child: Column(
              children: [
                /*
                FormBuilderDateTimePicker(
                  name: 'takenDate',
                  decoration: InputDecoration(labelText: '拍照日期'),
                  inputType: InputType.date,
                  format: DateFormat('y/M/d'),
                  initialEntryMode: DatePickerEntryMode.input,
                  initialDate: widget._image.takenDate,
                  initialValue: widget._image.takenDate,
                  valueTransformer: (v) => '${v!.year}-${v.month}-${v.day}',
                ),
                 */
                FormBuilderTextField(
                  name: 'takenDate',
                  decoration: InputDecoration(labelText: '拍照日期'),
                  initialValue:
                      DateFormat('y/M/d').format(widget._image.takenDate),
                  validator: FormBuilderValidators.compose([
                    FormBuilderValidators.required(),
                    myDateString(context),
                  ]),
                ),
                FormBuilderTextField(
                  name: 'description',
                  decoration: InputDecoration(labelText: '描述信息'),
                  minLines: 2,
                  maxLines: 5,
                  initialValue: widget._image.description,
                ),
              ],
            ),
          ),
          SizedBox(height: 10),
          FutureBuilder<List?>(
            future: loadTags(),
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                tagsSelector = TagsSelector(
                    tags: snapshot.data!,
                    initValues: widget._image.tags == null
                        ? []
                        : widget._image.tags!
                            .split(",")
                            .where((s) => s.isNotEmpty)
                            .toList());
                return tagsSelector;
              } else {
                return Text('loading...');
              }
            },
          ),
          SizedBox(height: 10),
          TextButton(
            onPressed: () {
              if (_formKey.currentState!.validate()) {
                _formKey.currentState!.save();
                var map = Map<String, dynamic>();
                map.addAll(_formKey.currentState!.value);
                map['tags'] = tagsSelector.selectedValues.join(",");
                widget._image.picsModel.updateImage(widget._image, map);
                showToast('已保存照片修改');
                if (detailViewKey.currentState != null) {
                  detailViewKey.currentState!.focusNode.requestFocus();
                }
              }
            },
            child: Text('保存'),
          ),
        ],
      ),
    );
  }

  Future<List> loadTags() async {
    var resp = await Dio().post(Config.api("/api/getAllTags"));
    if (resp.statusCode != 200) {
      showToast("获取tags失败 ${resp.data}");
      return [];
    }
    return resp.data as List;
  }

  static FormFieldValidator<String> myDateString(
    BuildContext context, {
    String? errorText,
  }) =>
      (valueCandidate) =>
          true == valueCandidate?.isNotEmpty && !isDate(valueCandidate!)
              ? errorText ??
                  FormBuilderLocalizations.of(context).dateStringErrorText
              : null;

  static bool isDate(String s) {
    try {
      DateFormat('y-M-d').parse(s);
      return true;
    } catch (e) {
      try {
        DateFormat('y/M/d').parse(s);
        return true;
      } catch (e) {
        return false;
      }
    }
  }
}
