# -*- coding: utf-8 -*-
# Generated by Django 1.11.9 on 2019-08-07 15:01
from __future__ import unicode_literals

from django.db import migrations, models
import django.utils.timezone


def forwards(apps, schema_editor):
    Team = apps.get_model('central', 'team')
    CTC = apps.get_model('central', 'trustcircle')
    for team in Team.objects.all():
        try:
            team.modified = team.history.latest().history_date
        except:
            team.modified = team.created
        team.save()

    for ctc in CTC.objects.all():
        try:
            ctc.modified = ctc.history.latest().history_date
        except:
            ctc.modified = ctc.created
        ctc.save()


class Migration(migrations.Migration):

    dependencies = [
        ('central', '0007_auto_20190731_1354'),
    ]

    operations = [
        migrations.AddField(
            model_name='historicalteam',
            name='modified',
            field=models.DateTimeField(blank=True, default=django.utils.timezone.now, editable=False, verbose_name='Modified on'),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='historicaltrustcircle',
            name='modified',
            field=models.DateTimeField(blank=True, default=django.utils.timezone.now, editable=False, verbose_name='Modified on'),
            preserve_default=False,
        ),
        migrations.AddField(
            model_name='team',
            name='modified',
            field=models.DateTimeField(auto_now=True, verbose_name='Modified on'),
        ),
        migrations.AddField(
            model_name='trustcircle',
            name='modified',
            field=models.DateTimeField(auto_now=True, verbose_name='Modified on'),
        ),
        migrations.AlterField(
            model_name='historicalteam',
            name='csp_domain',
            field=models.CharField(blank=True, max_length=255, verbose_name='CSP Domain (with CSP ID)'),
        ),
        migrations.AlterField(
            model_name='team',
            name='csp_domain',
            field=models.CharField(blank=True, max_length=255, verbose_name='CSP Domain (with CSP ID)'),
        ),

        migrations.RunPython(forwards, migrations.RunPython.noop),
    ]
